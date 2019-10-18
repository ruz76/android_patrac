package cz.vsb.gis.ruz76.android.patracmonitor.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StorageDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "patrac_database";
    public static final String LOCATION_TABLE_NAME = "location";
    public static final String LOCATION_COLUMN_ID = "_id";
    public static final String LOCATION_COLUMN_LAT = "lat";
    public static final String LOCATION_COLUMN_LON = "lon";
    public static final String LOCATION_COLUMN_TS = "ts";

    public static final String MESSAGES_RECEIVED_TABLE_NAME = "messages_received";
    public static final String MESSAGES_RECEIVED_COLUMN_ID = "_id";
    public static final String MESSAGES_RECEIVED_COLUMN_MESSAGEID = "messageid";
    public static final String MESSAGES_RECEIVED_COLUMN_FROMID = "fromid";
    public static final String MESSAGES_RECEIVED_COLUMN_MESSAGE = "message";
    public static final String MESSAGES_RECEIVED_COLUMN_FILE = "file";
    public static final String MESSAGES_RECEIVED_COLUMN_SEARCHID = "searchid";
    public static final String MESSAGES_RECEIVED_COLUMN_DT_CREATED = "dt_created";
    public static final String MESSAGES_RECEIVED_COLUMN_SHARED = "shared";
    public static final String MESSAGES_RECEIVED_COLUMN_READED = "readed";

    public StorageDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
                LOCATION_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LOCATION_COLUMN_LAT + " FLOAT, " +
                LOCATION_COLUMN_LON + " FLOAT, " +
                LOCATION_COLUMN_TS + " LONG" + ")");

        sqLiteDatabase.execSQL("CREATE TABLE " + MESSAGES_RECEIVED_TABLE_NAME + " (" +
                MESSAGES_RECEIVED_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MESSAGES_RECEIVED_COLUMN_MESSAGEID + " INTEGER, " +
                MESSAGES_RECEIVED_COLUMN_FROMID + " VARCHAR(50), " +
                MESSAGES_RECEIVED_COLUMN_MESSAGE + " VARCHAR(255), " +
                MESSAGES_RECEIVED_COLUMN_FILE + " VARCHAR(255), " +
                MESSAGES_RECEIVED_COLUMN_SEARCHID + " VARCHAR(20), " +
                MESSAGES_RECEIVED_COLUMN_DT_CREATED + " VARCHAR(20), " +
                MESSAGES_RECEIVED_COLUMN_SHARED + " INTEGER, " +
                MESSAGES_RECEIVED_COLUMN_READED + " INTEGER" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MESSAGES_RECEIVED_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
