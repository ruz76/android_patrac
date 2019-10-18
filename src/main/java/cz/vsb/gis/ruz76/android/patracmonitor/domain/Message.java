package cz.vsb.gis.ruz76.android.patracmonitor.domain;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

/**
 * Class for handling messages.
 */

public class Message {
    private int messageid;
    private String fromid;
    private String message;
    private String file;
    private String searchid;
    private String dt_created;
    private int shared;
    private int readed;

    public Message(int messageid, String fromid, String message, String file, String searchid, String dt_created, int shared, int readed) {
        this.messageid = messageid;
        this.fromid = fromid;
        this.message = message;
        this.file = file;
        this.searchid = searchid;
        this.dt_created = dt_created;
        this.shared = shared;
        this.readed = readed;
    }

    public Message(int messageid, String fromid, String message, String file, String dt_created) {
        this.messageid = messageid;
        this.fromid = fromid;
        this.message = message;
        this.file = file;
        this.dt_created = dt_created;
    }

    public Message(JSONObject message) {
        try {
            this.messageid = message.getInt("messageid");
            this.fromid = message.getString("fromid");
            this.message = message.getString("message");
            this.file = message.getString("file");
            this.searchid = message.getString("searchid");
            this.dt_created = message.getString("dt_created");
            this.shared = message.getInt("shared");
            this.readed = 0;
            Log.i("Message", message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getMessageId() {
        return messageid;
    }

    public void setMessageId(int messageid) {
        this.messageid = messageid;
    }

    public String getFromId() {
        return fromid;
    }

    public void setFromId(String fromid) {
        this.fromid = fromid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getSearchid() {
        return searchid;
    }

    public void setSearchid(String searchid) {
        this.searchid = searchid;
    }

    public String getDt_created() {
        return dt_created;
    }

    public void setDt_created(String dt_created) {
        this.dt_created = dt_created;
    }

    public int getShared() {
        return shared;
    }

    public void setShared(int shared) {
        this.shared = shared;
    }

    public int getReaded() {
        return readed;
    }

    public void setReaded(int readed) {
        this.readed = readed;
    }

    @Override
    public String toString() {
        return this.fromid + ": "
                + (this.message.length() > 10 ? this.message.substring(0, 10) + "..." : this.message)
                + (this.file.length() > 0 ? " @" : "")
                + " " + this.dt_created;
    }
}
