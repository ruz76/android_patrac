package cz.vsb.gis.ruz76.android.patracmonitor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

import cz.vsb.gis.ruz76.android.patracmonitor.domain.Message;

public class StorageDao {
    public static void saveLocationToDB(Location location, Context context) {
        SQLiteDatabase database = new StorageDBHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StorageDBHelper.LOCATION_COLUMN_LAT, location.getLatitude());
        values.put(StorageDBHelper.LOCATION_COLUMN_LON, location.getLongitude());
        values.put(StorageDBHelper.LOCATION_COLUMN_TS, location.getTime());
        database.insert(StorageDBHelper.LOCATION_TABLE_NAME, null, values);
        database.close();
    }

    public static String getLocations(Context context, int lastid) {
        SQLiteDatabase database = new StorageDBHelper(context).getReadableDatabase();

        String[] projection = {
                StorageDBHelper.LOCATION_COLUMN_ID,
                StorageDBHelper.LOCATION_COLUMN_LAT,
                StorageDBHelper.LOCATION_COLUMN_LON,
                StorageDBHelper.LOCATION_COLUMN_TS
        };

        String selection =
                StorageDBHelper.LOCATION_COLUMN_ID + "  > ?";

        String[] selectionArgs = {String.valueOf(lastid)};

        Cursor cursor = database.query(
                StorageDBHelper.LOCATION_TABLE_NAME,   // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // don't sort
        );

        String locations = "";
        int count = 0;
        while (cursor.moveToNext()) {
            if (count > 0) {
                locations += ", ";
            }
            locations += "{\"id\":" + cursor.getInt(0);
            locations += ",\"lat\":" + cursor.getFloat(1);
            locations += ",\"lon\":" + cursor.getFloat(2);
            locations += ",\"ts\":" + cursor.getLong(3);
            locations += "}";
            count++;
        }
        if (locations.length() > 0) {
            locations = "[" +  locations + "]";
        }
        cursor.close();
        database.close();
        return locations;
    }

    public static void saveReceivedMessageToDB(Message message, Context context) {
        SQLiteDatabase database = new StorageDBHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StorageDBHelper.MESSAGES_RECEIVED_COLUMN_MESSAGEID, message.getMessageId());
        values.put(StorageDBHelper.MESSAGES_RECEIVED_COLUMN_FROMID, message.getFromId());
        values.put(StorageDBHelper.MESSAGES_RECEIVED_COLUMN_MESSAGE, message.getMessage());
        values.put(StorageDBHelper.MESSAGES_RECEIVED_COLUMN_FILE, message.getFile());
        values.put(StorageDBHelper.MESSAGES_RECEIVED_COLUMN_SEARCHID, message.getSearchid());
        values.put(StorageDBHelper.MESSAGES_RECEIVED_COLUMN_DT_CREATED, message.getDt_created());
        values.put(StorageDBHelper.MESSAGES_RECEIVED_COLUMN_SHARED, message.getShared());
        values.put(StorageDBHelper.MESSAGES_RECEIVED_COLUMN_READED, message.getReaded());
        //Log.i("saveReceivedMessageToDB", values.toString());
        database.insert(StorageDBHelper.MESSAGES_RECEIVED_TABLE_NAME, null, values);
        //Log.i("saveDB - DB", StorageDao.getMessages(context, message.getSearchid()).toString());
        database.close();
    }

    public static ArrayList<Message> getMessages(Context context, String searchid) {
        SQLiteDatabase database = new StorageDBHelper(context).getReadableDatabase();

        String[] projection = {
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_MESSAGEID,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_FROMID,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_MESSAGE,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_FILE,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_DT_CREATED
        };

        String selection =
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_SEARCHID + " = ?";

        String[] selectionArgs = {searchid};

        Cursor cursor = database.query(
                StorageDBHelper.MESSAGES_RECEIVED_TABLE_NAME,   // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // don't sort
        );

        ArrayList<Message> messages = new ArrayList<>();
        while (cursor.moveToNext()) {
            Message message = new Message(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            messages.add(message);
        }
        cursor.close();
        database.close();
        return messages;
    }

    public static Message getMessage(Context context, int messageid) {
        SQLiteDatabase database = new StorageDBHelper(context).getReadableDatabase();

        String[] projection = {
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_MESSAGEID,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_FROMID,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_MESSAGE,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_FILE,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_SEARCHID,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_DT_CREATED,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_SHARED,
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_READED,
        };

        String selection =
                StorageDBHelper.MESSAGES_RECEIVED_COLUMN_MESSAGEID + " = ?";

        String[] selectionArgs = {String.valueOf(messageid)};

        Cursor cursor = database.query(
                StorageDBHelper.MESSAGES_RECEIVED_TABLE_NAME,   // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // don't sort
        );

        cursor.moveToNext();
        return new Message(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getInt(7));
    }
}
