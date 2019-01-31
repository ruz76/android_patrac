/*
 * Based on The Android Open Source Project
 * com.example.android.networkconnect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.vsb.gis.ruz76.patrac.android.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cz.vsb.gis.ruz76.patrac.android.R;
import cz.vsb.gis.ruz76.patrac.android.domain.Message;
import cz.vsb.gis.ruz76.patrac.android.domain.RequestMode;
import cz.vsb.gis.ruz76.patrac.android.domain.Status;
import cz.vsb.gis.ruz76.patrac.android.domain.Waypoint;
import cz.vsb.gis.ruz76.patrac.android.helpers.AdapterHelper;
import cz.vsb.gis.ruz76.patrac.android.helpers.DownloadFileFromUrl;
import cz.vsb.gis.ruz76.patrac.android.helpers.GetRequest;
import cz.vsb.gis.ruz76.patrac.android.helpers.GetRequestUpdate;
import cz.vsb.gis.ruz76.patrac.android.helpers.LogHelper;
import cz.vsb.gis.ruz76.patrac.android.helpers.Network;
import cz.vsb.gis.ruz76.patrac.android.helpers.Notificator;
import cz.vsb.gis.ruz76.patrac.android.receivers.PositionReceiver;

/**
 * Main Activity.
 */
public class MainActivity extends Activity implements LocationListener, GetRequestUpdate {

    private Menu menu;

    public static boolean processingRequest = false;

    private TextView mDataText;
    private TextView mStatusText;
    private ListView messagesListView;
    private TextView mWelcomeText;
    private Intent callOnDuty;

    private double latFromListener = 0;
    private double lonFromListener = 0;

    private int lastLoggedPositionId = 0;

    Timer timerPosition;
    PositionTask myPositionTask;
    Timer timerMessage;
    MessageTask myMessageTask;
    Timer timerSearch;
    SearchTask mySearchTask;
    Timer timerCallOnDuty;
    CallOnDutyTask myCallOnDutyTask;
    SharedPreferences sharedPrefs;

    protected Context context;
    protected LocationManager locationManager;
    private Notificator notificator = new Notificator();

    // Create an ArrayAdapter from List
    private ArrayAdapter<String> arrayAdapter;

    //Methods from Location Listener
    @Override
    public void onLocationChanged(Location location) throws SecurityException {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (locationGPS != null) {
            latFromListener = locationGPS.getLatitude();
            lonFromListener = locationGPS.getLongitude();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    /**
     * Creates instances from saved state.
     * Sets permissions or requests permissions.
     * Sets gui based on layout activity_main.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) throws SecurityException {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR);

        Calendar c = Calendar.getInstance();
        Log.i("AlarmManager", String.valueOf(c.getTimeInMillis() + 10000));

        setPermissions();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        extractGuiItems();
        mDataText.setText(R.string.mode_sleeping);
        setSearchTimer();
        context = this.getApplicationContext();
        notificator.setContext(context);
        Status.endPoint = sharedPrefs.getString("endpoint", getString(R.string.pref_default_endpoint));
        Status.sessionId = sharedPrefs.getString("sessionid", null);
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        setMessagesAdapter();
        setStatusTextAdapter();
        if (RequestMode.TRACKING == Status.mode) {
            connect();
        }

        Intent ll24 = new Intent(context, PositionReceiver.class);
        PendingIntent recurringLl24 = PendingIntent.getBroadcast(context, 0, ll24, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarms.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + 10000, 60000, recurringLl24);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt("restored", Status.restored);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        Status.restored = savedInstanceState.getInt("restored") + 1;
        Calendar c = Calendar.getInstance();
        Log.i("onRestoreInstanceState", String.valueOf(c.getTimeInMillis() + 1000));
    }

    private void setStatusTextAdapter() {
        mStatusText.setMovementMethod(new ScrollingMovementMethod());
        mStatusText.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                Intent logView = new Intent(MainActivity.this, LogActivity.class);
                startActivity(logView);
                return false;
            }
        });
    }

    /**
     * Ads items to the menu.
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    /**
     * Invoked on menu click.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect_disconnect_action:
                if (!Status.connected) {
                    resetItems();
                    connect();
                } else {
                    disconnect();
                }
                return true;
            case R.id.clear_action:
                return startActivity(MainActivity.this, SettingsActivity.class);

            case R.id.map_action:
                return startActivity(MainActivity.this, MapsActivity.class);

            case R.id.send_message_action:
                return startActivity(MainActivity.this, MessageSendActivity.class);

        }
        return false;
    }

    /**
     * Extracts gui items to variables to be better accessible.
     */
    private void extractGuiItems() {
        mDataText = (TextView) findViewById(R.id.data_text);
        mStatusText = (TextView) findViewById(R.id.status_text);
        messagesListView = (ListView) findViewById(R.id.messagesListView);
        mWelcomeText = (TextView) findViewById(R.id.intro_text);
        String systemid = sharedPrefs.getString("id", null);
        if (systemid != null && !systemid.isEmpty()) {
            mWelcomeText.setText(getString(R.string.welcome_message_registered));
        }
        if (!Network.getInstance().isNetworkAvailable()) {
            mWelcomeText.append("\n" + getString(R.string.turn_on_internet_connection));
        }
    }

    /**
     * Sets or requests permissions for the application.
     */
    private void setPermissions() {
        boolean permissionsNotSets = true;
        while (permissionsNotSets) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNotSets = true;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                permissionsNotSets = false;
            }
        }
    }


    /**
     * Resets items to empty or zero values.
     */
    private void resetItems() {
        Status.lat = 0;
        Status.lon = 0;
        Status.positionCount = 0;
        Status.loggedPositionCount = 0;
        Status.sendPositionCount = 0;
        Status.messagesCount = 0;
        Status.errorsCount = 0;

        //Status.sessionId = null;
        Status.waypoints = new ArrayList<>();
        Status.searchid = sharedPrefs.getString("searchid", null);
    }

    private void startTracking() {
        Status.mode = RequestMode.TRACKING;
        Status.searchid = sharedPrefs.getString("searchid", null);
    }

    private void setMessagesAdapter() {
        arrayAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, Status.messages_list);

        // DataBind ListView with items from ArrayAdapter
        messagesListView.setAdapter(arrayAdapter);

        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent appInfo = new Intent(MainActivity.this, MessageViewActivity.class);
                appInfo.putExtra("message", Status.messages_list_full.get(position).getMessage());
                appInfo.putExtra("filename", Status.messages_list_full.get(position).getFilename());
                appInfo.putExtra("from", Status.messages_list_full.get(position).getFrom());
                startActivity(appInfo);
            }

        });
    }

    /**
     * It is triggered when the download of the data from Download is finished.
     *
     * @param result response from the server
     */
    public void processResponse(String result) {
        LogHelper.i("RESULT", result);
        processingRequest = false;
        if (result != null && !result.isEmpty()) {
            String responseCode = result.substring(0, 1);
            switch (responseCode) {
                case "R":
                    goToSleep();
                    break;
                case "S":
                case "W":
                    processSearchesResponse(result);
                    break;
                case "D":
                    processCallOnDutyResponse(result);
                    break;
                case "J":
                    processReadyForTrackingResponse(result);
                    break;
                case "A":
                    processSelectedResponse(result);
                    break;
                case "P":
                case "M":
                case "I":
                    processTrackingResponse(result);
                case "T":
                    processStatusResponse(result);
            }
        } else {
            showErrorConnection();
        }
    }

    /**
     *
     * @param result result to process
     */
    private void processTrackingResponse(String result) {
        if (result.startsWith("ID:")) {
            // We have obtained session id, so the first location is saved as well
            Status.sendPositionCount++;
            Status.sessionId = result.substring(3);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("sessionid", Status.sessionId);
            editor.commit();
        } else if (result.startsWith("M")) {
            // We have obtained new message
            if (result.split(";").length == 8) {
                processMessage(result);
            }
        } else if (result.startsWith("P")) {
            // We have obtained information about number of saved positions
            // We do not check it, it should be OK.
            Status.sendPositionCount = Status.loggedPositionCount;
            setInfo();
        }
    }

    /**
     *
     * @param result result to process
     */
    private void processStatusResponse(String result) {
        if (result.startsWith("T;onduty") && RequestMode.TRACKING != Status.mode) {
            String[] items = result.split(";");
            if (items.length == 3) {
                Status.searchid = items[2];
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("searchid", Status.searchid);
                editor.commit();
                connect();
            }
        }
        if (result.startsWith("T;waiting") && RequestMode.WAITING != Status.mode) {
            Status.mode = RequestMode.WAITING;
            mDataText.setText(R.string.mode_waiting);
            // starts new timer with more often check
            setCallOnDutyTimer();
        }
    }

    private void showErrorConnection() {
        //The response is null
        Status.errorsCount++;
        /*DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        mStatusText.append("\n" + getString(R.string.connection_error) + " v " + dateFormat.format(date) + ". Celkem chyb: " + Status.errorsCount);
        */
        LogHelper.e("ERROR", "Result is null");
        setInfo();
    }

    private void processReadyForTrackingResponse(String result) {
        // Call to join
        notificator.playRing();
        Toast toast = Toast.makeText(MainActivity.this, R.string.on_duty, Toast.LENGTH_LONG);
        toast.show();
        resetItems();
        connect();
        if (result.split(";").length > 2) {
            Status.searchDescription = result.split(";")[2];
        }
        mWelcomeText.setText(getString(R.string.on_duty) + " " + Status.searchDescription);
        menu.findItem(R.id.connect_disconnect_action).setTitle(R.string.disconnect);
    }

    /**
     * Reads response.
     *
     * @param result result to process
     */
    private void processSearchesResponse(String result) {
        if (RequestMode.SLEEPING == Status.mode) {
            if (result.startsWith("S")) {
                // there is a new search
                String id = sharedPrefs.getString("id", null);
                if (id != null && !id.isEmpty() && !processingRequest && Network.getInstance().isNetworkAvailable()) {
                    sendGetRequest(Status.endPoint + "operation=searches&id=" + id + "&lat=" + getShortCoord(latFromListener) + "&lon=" + getShortCoord(lonFromListener));
                }
            }
            if (result.startsWith("W")) {
                // the coordinates where saved at the server
                Status.mode = RequestMode.WAITING;
                mDataText.setText(R.string.mode_waiting);
                // starts new timer with more often check
                setCallOnDutyTimer();
            }
        }
    }

    /**
     * Reads response in waiting mode.
     * May contain three states: list, empty, null
     * list - list of active searches on the server
     * empty - no searches on call
     * null - some error occurred - nothing to do
     *
     * @param result result to process
     */
    private void processCallOnDutyResponse(String result) {
        String[] searches = result.split("\n");
        notificator.playRing();
        callOnDuty = new Intent(MainActivity.this, CallOnDutyActivity.class);
        callOnDuty.putExtra("searches", searches);
        startActivity(callOnDuty);
    }

    /**
     * Reads response in selected mode. User has selected the search.
     * May contain two states: some data, null
     * some data - server knows that you are ready
     * null - some error occurred - nothing to do TODO - do something, otherwise the call is not connected
     *
     * @param result result to process
     */
    private void processSelectedResponse(String result) {
        // the searchid was saved at the server
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("searchid", Status.searchid);
        editor.commit();
        Status.mode = RequestMode.READY_FOR_TRACKING;
        mDataText.setText(R.string.mode_ready_for_tracking);
    }

    /**
     * Timer for sleeping and waiting mode.
     */
    private void setSearchTimer() {
        if (timerSearch != null) {
            timerSearch.cancel();
        }

        timerSearch = new Timer();
        mySearchTask = new SearchTask();
        timerSearch.schedule(mySearchTask, 30000, 1000 * 60 * 1);
    }

    /**
     * Timer for call on duty state.
     */
    private void setCallOnDutyTimer() {
        if (timerCallOnDuty != null) {
            timerCallOnDuty.cancel();
        }

        timerCallOnDuty = new Timer();
        myCallOnDutyTask = new CallOnDutyTask();
        timerCallOnDuty.schedule(myCallOnDutyTask, 0, 1000 * 10);
    }

    private void goToSleep() {
        String id = sharedPrefs.getString("id", null);

        // inform server about sleeping mode
        if (id != null && !processingRequest && Network.getInstance().isNetworkAvailable()) {
            sendGetRequest(Status.endPoint + "operation=changestatus&status_to=sleeping&id=" + id);
        }

        // remove searchid
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("searchid", null);
        editor.commit();
        Status.searchid = null;

        if (RequestMode.SLEEPING != Status.mode) {
            // move to Sleeping mode
            disconnect();
            menu.findItem(R.id.connect_disconnect_action).setTitle(R.string.connect);

        }
    }

    /**
     * Checks new search.
     */
    private void checkSearches() {
        String id = sharedPrefs.getString("id", null);
        if (id != null
                && !id.isEmpty()
                && !processingRequest
                && Network.getInstance().isNetworkAvailable()
                && (RequestMode.SLEEPING == Status.mode
                || RequestMode.READY_FOR_TRACKING == Status.mode
                || RequestMode.TRACKING == Status.mode)) {
            sendGetRequest(Status.endPoint + "operation=searches&id=" + id);
        }
        if (!Network.getInstance().isNetworkAvailable()) {
            Toast toast = Toast.makeText(MainActivity.this, R.string.turn_on_internet_connection, Toast.LENGTH_LONG);
            toast.show();
        }
        if (id != null && !id.isEmpty() && RequestMode.SLEEPING == Status.mode) {
            mWelcomeText.setText(getString(R.string.welcome_message_registered));
        }
        if (id != null && !id.isEmpty() && RequestMode.TRACKING == Status.mode && !Status.searchDescription.isEmpty()) {
            mWelcomeText.setText(getString(R.string.on_duty) + " " + Status.searchDescription);
        }
        checkCallOnDuty();
    }

    /**
     * Check call on duty request.
     */
    public void checkCallOnDuty() {
        String id = sharedPrefs.getString("id", null);
        if (id != null && !id.isEmpty() && !processingRequest && Network.getInstance().isNetworkAvailable()) {
            switch (Status.mode) {
                case WAITING:
                    sendGetRequest(Status.endPoint + "operation=searches&id=" + id);
                    break;
                case SELECTED:
                    sendGetRequest(Status.endPoint + "operation=searches&id=" + id + "&searchid=" + Status.searchid + "&arrive=" + Status.arrive);
                    break;
                case READY_FOR_TRACKING:
                    sendGetRequest(Status.endPoint + "operation=searches&id=" + id);
                    break;
            }
        }
    }


    /**
     * Initialize the connection.
     */
    private void connect() {
        if (Status.searchid != null && !Status.searchid.isEmpty()) {
            getSessionId();
            startTracking();
            menu.findItem(R.id.connect_disconnect_action).setTitle(R.string.disconnect);

            // Sets the timer for logging position.
            if (timerPosition != null) {
                timerPosition.cancel();
            }
            timerPosition = new Timer();
            myPositionTask = new PositionTask();
            String sync_frequency_gps_string = sharedPrefs.getString("sync_frequency_gps", "5");
            int sync_frequency_gps = Integer.parseInt(sync_frequency_gps_string) * 1000;
            timerPosition.schedule(myPositionTask, 1000, sync_frequency_gps);


            // Sets the times for checking messages.
            if (timerMessage != null) {
                timerMessage.cancel();
            }

            timerMessage = new Timer();
            myMessageTask = new MessageTask();
            String sync_frequency_string = sharedPrefs.getString("sync_frequency", "30");
            int sync_frequency = Integer.parseInt(sync_frequency_string) * 1000;
            timerMessage.schedule(myMessageTask, 30000, sync_frequency);

            // set the state to connected
            Status.connected = true;
        } else {
            Toast toast = Toast.makeText(MainActivity.this, R.string.no_searchid, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * Stops timers.
     */
    private void disconnect() {
        menu.findItem(R.id.connect_disconnect_action).setTitle(R.string.connect);
        Status.connected = false;
        if (timerPosition != null) {
            timerPosition.cancel();
        }
        if (timerMessage != null) {
            timerMessage.cancel();
        }
        callOnDuty = null;
        Status.mode = RequestMode.SLEEPING;
        Status.searchDescription = "";
        mDataText.setText(R.string.mode_sleeping);
        String systemid = sharedPrefs.getString("id", null);
        if (systemid != null && !systemid.isEmpty()) {
            mWelcomeText.setText(getString(R.string.welcome_message_registered));
        } else {
            mWelcomeText.setText(getString(R.string.welcome_message_anonymous));
        }
    }

    /**
     * Requests for session id.
     */
    private void getSessionId() {
        //Maybe put phone number instead of NN and random
        String systemid = sharedPrefs.getString("id", null);
        String user_name = sharedPrefs.getString("user_name", "NN " + Math.round(Math.random() * 10000));
        String sessionid = sharedPrefs.getString("sessionid", null);
        try {
            if (Network.getInstance().isNetworkAvailable() && !processingRequest) {
                sendGetRequest(Status.endPoint + "operation=getid&id=" + sessionid + "&searchid="
                        + Status.searchid + "&user_name=" + URLEncoder.encode(user_name, "UTF-8")
                        + "&systemid=" + systemid + "&lat=" + getShortCoord(Status.lat) + "&lon=" + getShortCoord(Status.lon));
            }
        } catch (UnsupportedEncodingException e) {
            LogHelper.e("UnsupportedEncoding", e.getMessage());
        }
    }

    /**
     * Reads location from system and compares it to the previous position.
     *
     * @return true if the position should be logged
     * @throws SecurityException
     */
    private boolean trackLocation() throws SecurityException {
        double newlat = 0;
        double newlon = 0;
        boolean logit = false;
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (locationGPS == null) {
            // we do not have location yet, so wait for location to the next interval
            locationGPS = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (locationGPS == null) {
                mDataText.setText(getString(R.string.no_location));
                return false;
            }
        }
        newlat = locationGPS.getLatitude();
        newlon = locationGPS.getLongitude();
        Status.positionCount++;
        //TODO do it better
        if (Math.hypot(Status.lat - newlat, Status.lon - newlon) >= 0.0001) { //0.0001
            Status.lat = newlat;
            Status.lon = newlon;
            logit = true;
            Waypoint wp = new Waypoint(newlat, newlon, new Date());
            Status.waypoints.add(wp);
            Status.loggedPositionCount++;
            lastLoggedPositionId++;
        }
        setInfo();
        return logit;
    }

    /**
     * Sends current location or cached 100 locations to the server.
     */
    private void sendTrack() {
        if ((Status.sendPositionCount == (Status.loggedPositionCount - 1)) && Network.getInstance().isNetworkAvailable()) {
            sendGetRequest(Status.endPoint + "operation=sendlocation&searchid=" + Status.searchid
                    + "&id=" + Status.sessionId + "&lat=" + getShortCoord(Status.lat) + "&lon=" + getShortCoord(Status.lon));
        }
        if ((Status.sendPositionCount < (Status.loggedPositionCount - 1)) && Network.getInstance().isNetworkAvailable()) {
            //Some time out of network. Must send more coords from memory.
            //TODO change to POST to be able send more than 100 coords
            String notSendedCoords = "";
            int startPosition = Status.sendPositionCount + 1;
            //When is more than 50 not sent positions in memory then points are generalized to last 100 is sent to server
            if ((Status.waypoints.size() - startPosition) > 50) {
                int step = (int) Math.ceil((Status.waypoints.size() - startPosition) / 50);
                for (int i = startPosition; i < Status.waypoints.size(); i += step) {
                    Waypoint wp = Status.waypoints.get(i);
                    notSendedCoords += getShortCoord(wp.getLon()) + ";" + getShortCoord(wp.getLat()) + ",";
                }
            } else {
                for (int i = startPosition; i < Status.waypoints.size(); i++) {
                    Waypoint wp = Status.waypoints.get(i);
                    notSendedCoords += getShortCoord(wp.getLon()) + ";" + getShortCoord(wp.getLat()) + ",";
                }
            }
            if (notSendedCoords.length() > 10) {
                notSendedCoords = notSendedCoords.substring(0, notSendedCoords.length() - 2);
                sendGetRequest(Status.endPoint + "operation=sendlocations&searchid=" + Status.searchid
                        + "&id=" + Status.sessionId + "&coords=" + notSendedCoords);
            }
        }
    }

    /**
     * Shows the information that the posision is the same.
     */
    private void showInfoSamePosition() {
        setInfo();
        String content = String.valueOf(mDataText.getText());
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        content += " " + getString(R.string.same_position_label) + ": " + dateFormat.format(date);
        mDataText.setText(content);
    }

    /**
     * Sends coordinates to the server.
     *
     * @throws SecurityException
     */
    private void sendCoordinates() throws SecurityException {
        boolean logit = trackLocation();
        if (Status.StatusMessages != null) {
            //mStatusText.append("\n" + Status.StatusMessages);
            Status.StatusMessages = null;
        }
        // if the position has not changed we do not send it to the server
        if (logit || (Status.sendPositionCount <= Status.loggedPositionCount)) {
            if (!processingRequest) {
                sendTrack();
            }
        } else {
            showInfoSamePosition();
        }
    }

    /**
     * Flat the coordinate to has just 6 decimal points.
     *
     * @param coord coordinate to flat
     * @return flatted coordinate
     */
    private String getShortCoord(double coord) {
        String coordLong = Double.toString(coord);
        String parts[] = coordLong.split("\\.");
        if (parts[1].length() > 6) {
            return parts[0] + "." + parts[1].substring(0, 6);
        } else {
            return parts[0] + "." + parts[1];
        }
    }

    /**
     * Starts downloading a message.
     */
    private void downloadMessage() {
        // we do not have session id yet, so ask for it
        if (Status.sessionId == null && !processingRequest && Network.getInstance().isNetworkAvailable()) {
            getSessionId();
        } else {
            boolean messages_switch = sharedPrefs.getBoolean("messages_switch", true);
            if (messages_switch && !processingRequest && Network.getInstance().isNetworkAvailable()) {
                sendGetRequest(Status.endPoint + "operation=getmessages&searchid=" + Status.searchid + "&id=" + Status.sessionId);
            }
        }
    }

    /**
     * Process the new message. If there is an attachment it is downloaded.
     *
     * @param result reponse from the servert
     */
    private void processMessage(String result) {
        // New message is on the way
        // Structure
        /*
         M;id;message;attachment;shared;from;sysid
         0 M (code)
         1 id
         2 message
         3 attachment
         4 datetime
         5 shared
         6 from
         7 sysid
         */
        Status.messagesCount++;
        String items[] = result.split(";");
        Message mf = new Message(getString(R.string.message_when) + ": " + items[4] + "\n" + getString(R.string.message) + ": " + items[2], items[3], items[6]);
        Status.messages_list_full.add(0, mf);
        String short_message = items[2].length() > 20 ? items[2].substring(0, 20) + "..." : items[2];
        if (items[3].length() > 1) {
            Status.messages_list.add(0, items[6] + ": " + items[4].substring(0, items[4].length() - 3).split(" ")[1] + ": " + short_message + " (@)");
            //Shared file
            String shared = items[5].replace("\n", "");
            if (shared.equalsIgnoreCase("1")) {
                if (Network.getInstance().isNetworkAvailable()) {
                    downloadFromUrl(Status.endPoint + "operation=getfile&searchid=" + Status.searchid + "&id=shared&filename=" + items[3], items[3]);
                }
            } else {
                //Individual file
                if (Network.getInstance().isNetworkAvailable()) {
                    downloadFromUrl(Status.endPoint + "operation=getfile&searchid=" + Status.searchid
                            + "&id=" + Status.sessionId + "&filename=" + items[3], items[3]);
                }
            }
        } else {
            Status.messages_list.add(0, items[6] + ": " + items[4].substring(0, items[4].length() - 3).split(" ")[1] + ": " + short_message);
        }
        new AdapterHelper().update((ArrayAdapter) arrayAdapter, new ArrayList<Object>(Status.messages_list));
        arrayAdapter.notifyDataSetChanged();
        notificator.playRing();

        if (Status.sessionId != null && !processingRequest && Network.getInstance().isNetworkAvailable()) {
            sendGetRequest(Status.endPoint + "operation=markmessage"
                    + "&id=" + Status.sessionId
                    + "&searchid=" + Status.searchid
                    + "&sysid=" + items[7]);
        }
    }

    /**
     * Shows the information in the status bar.
     */
    private void setInfo() {
        String content = getString(R.string.sessionid_label) + ": "
                + Status.sessionId + " "
                + getString(R.string.searchid_label)
                + ": " + Status.searchid + "\n";
        content += getString(R.string.positions_label)
                + ": " + Status.positionCount
                + "/" + Status.loggedPositionCount
                + "/" + Status.sendPositionCount + "\n";
        content += getString(R.string.longitude_label) + ": "
                + getShortCoord(Status.lon)
                + " " + getString(R.string.latitude_label) + ": "
                + getShortCoord(Status.lat)
                + "\n";
        mDataText.setText(content);
    }

    /**
     * Starts the new activity.
     *
     * @param packageContext context of the package
     * @param appToStart     application to start
     * @return true
     */
    private boolean startActivity(Context packageContext, Class<?> appToStart) {
        Intent appToStartIntent = new Intent(packageContext, appToStart);
        startActivity(appToStartIntent);
        return true;
    }

    /**
     * Downloads file from url.
     *
     * @param url  url path to file
     * @param file filename where to save it
     */
    private void downloadFromUrl(String url, String file) {
        DownloadFileFromUrl downloadFileFromUrl = new DownloadFileFromUrl();
        downloadFileFromUrl.execute(url, file);
    }

    /**
     * Sends request via GET to url.
     *
     * @param url where to send
     */
    private void sendGetRequest(String url) {
        GetRequest getRequest = new GetRequest();
        getRequest.setActivity(this);
        processingRequest = true;
        getRequest.execute(url);
        LogHelper.i("REQUEST", url.substring(url.indexOf("?")));
    }

    /**
     * Timer task for synchronization of positions.
     */
    class PositionTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    sendCoordinates();
                }
            });
        }

    }

    /**
     * Timer task for synchronization of messages.
     */
    class MessageTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    downloadMessage();
                }
            });
        }

    }

    /**
     * Timer task for synchronization of messages.
     */
    class SearchTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    checkSearches();
                }
            });
        }

    }

    /**
     * Timer task for synchronization of messages.
     */
    class CallOnDutyTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    checkCallOnDuty();
                }
            });
        }

    }
}

