package cz.vsb.gis.ruz76.patrac.android.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jencek on 31.12.18.
 */

public class Status {
    public static RequestMode mode = RequestMode.SLEEPING;

    public static String sessionId = null;
    public static String searchid = null;
    public static String arrive = "NKD";
    public static String endPoint;
    public static String StatusMessages = null;
    public static ArrayList<Waypoint> waypoints = new ArrayList<>();;
    public static double lat = 0;
    public static double lon = 0;
    public static String searchDescription = "";

    public static int positionCount = 0;
    public static int loggedPositionCount = 0;
    public static int sendPositionCount = 0;
    public static int messagesCount = 0;
    public static int errorsCount = 0;
    public static boolean connected = false;

    // Create a List from String Array elements
    public static List<String> messages_list = new ArrayList<>();;
    public static List<Message> messages_list_full = new ArrayList<>();;

    public static int restored = 0;

}
